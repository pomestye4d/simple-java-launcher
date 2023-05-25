/*
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.vga.sjl.control;

import com.vga.sjl.utils.SjlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.logging.Logger;


public final class SjlControlThread extends Thread {
    private final ServerSocket serverSocket;
    private boolean appRunning;
    private final Callable<Void> stopCallback;
    private volatile boolean restartApp;


    interface RequestHandler {

        byte[] getRequest();

        boolean handleResponse(String response);

    }

    static boolean makeRequest(int port, RequestHandler handler) {
        try {
            InetAddress host = InetAddress.getByName("localhost");
            try (Socket socket = new Socket(host, port)) {
                socket.setKeepAlive(true);
                InputStream in = null;
                try (OutputStream out = socket.getOutputStream()) {
                    println("found running control service on " + host + ":" + port);
                    out.write(handler.getRequest());
                    out.flush();
                    socket.shutdownOutput();
                    in = socket.getInputStream();
                    StringBuilder commandResult = new StringBuilder();
                    byte[] buf = new byte[16];

                    int len;
                    while ((len = in.read(buf)) != -1) {
                        commandResult.append(new String(buf, 0, len));
                    }

                    socket.shutdownInput();
                    String result = commandResult.toString();
                    return handler.handleResponse(result);
                } finally {
                    if (in != null) in.close();
                }
            }
        } catch (IOException e) {
            println("seems that there is no control service running on localhost" + ": " + port);
            return false;
        }
    }

    public static boolean isApplicationRunning(int port) {

        return makeRequest(port, new RequestHandler() {
            private final String test = "" + System.currentTimeMillis();

            @Override
            public byte[] getRequest() {
                return ("PING " + test).getBytes();
            }

            @Override
            public boolean handleResponse(String response) {
                if (response.startsWith("OK") && response.contains(test)) {
                    println("PING command succeed");
                    return true;
                } else {
                    println("PING command failed");
                    return false;
                }
            }
        });
    }

    public static boolean stopRunningApplication(int port) {
        return makeRequest(port, new RequestHandler() {
            @Override
            public byte[] getRequest() {
                return "STOP".getBytes();
            }

            @Override
            public boolean handleResponse(String response) {
                if (response.startsWith("OK")) {
                    println("STOP command succeed");
                    return true;
                } else {
                    println("STOP command failed");
                    return false;
                }
            }
        });
    }

    public SjlControlThread(int port, Callable<Void> stopCallback) throws Exception {
        InetAddress host = InetAddress.getByName("localhost");
        this.serverSocket = new ServerSocket(port, 1, host);
        this.appRunning = true;
        this.stopCallback = stopCallback;
        this.setName("sjl-application-control-thread");
    }

    public void run() {
        try {
            while (true) {
                try {
                    try (Socket clientSocket = this.serverSocket.accept()) {
                        if (this.handleRequest(clientSocket)) {
                            break;
                        }
                    } catch (Exception e) {
                        //noops
                    }
                } catch (Exception e) {
                    println(SjlUtils.prepareLogMessage("error on server socket", e));
                    break;
                }
            }
        } finally {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                println(SjlUtils.prepareLogMessage("error closing server socket", e));
            }

            if (this.appRunning) {
                this.stopApplication();
            }
        }
        if (restartApp) {
            System.exit(2);
        }
    }

    private synchronized boolean handleRequest(Socket clientSocket) {
        try {
            if (!this.isValidHost(clientSocket.getInetAddress())) {
                println("incoming connection to control socket registered from REMOTE address " + clientSocket.getInetAddress() + ", attempt to execute command was IGNORED");

                try {
                    clientSocket.close();
                } catch (IOException e) {
                    //nopps
                }

                return false;
            }
        } catch (Throwable t) {
            return false;
        }


        boolean result = false;

        OutputStream out = null;
        try (InputStream in = clientSocket.getInputStream()) {
            StringBuilder command = new StringBuilder();
            byte[] buf = new byte[16];

            int len;
            while ((len = in.read(buf)) != -1) {
                command.append(new String(buf, 0, len));
            }

            clientSocket.shutdownInput();
            String commandResult;
            if ("STOP".equals(command.toString())) {
                this.stopApplication();
                result = true;
                commandResult = "OK: stop done";
            } else if ("RESTART".equals(command.toString())) {
                this.stopApplication();
                result = true;
                restartApp = true;
                commandResult = "RESTART: restart done";
            } else if (command.toString().startsWith("PING")) {
                commandResult = "OK: " + command.substring("PING".length());
            } else {
                commandResult = "ERROR: unknown command";
            }

            out = clientSocket.getOutputStream();
            out.write(commandResult.getBytes());
            out.flush();
            clientSocket.shutdownOutput();

        } catch (IOException e) {
            println(SjlUtils.prepareLogMessage("error processing control request", e));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    println(SjlUtils.prepareLogMessage("error closing socket", e));
                }
            }
        }

        return result;
    }

    private void stopApplication() {
        if (!this.appRunning) {
            println("application not running");
            return;
        }

        this.appRunning = false;
        println("stopping application");

        try {
            stopCallback.call();
        } catch (Exception e) {
            println(SjlUtils.prepareLogMessage("an error has occurred while stopping application", e));
            return;
        }
        println("application stopped from control thread");
    }

    private boolean isValidHost(InetAddress address) {
        byte[] localAddr = this.serverSocket.getInetAddress().getAddress();
        byte[] remoteAddr = address.getAddress();
        if (localAddr.length != remoteAddr.length) {
            return false;
        } else {
            for (int i = 0; i < remoteAddr.length; ++i) {
                if (localAddr[i] != remoteAddr[i]) {
                    return false;
                }
            }

            return true;
        }
    }



    private static void println(String text) {
        Logger.getLogger(SjlControlThread.class.getName()).info(text);
    }
}
