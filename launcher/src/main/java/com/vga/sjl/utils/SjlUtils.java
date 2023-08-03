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

package com.vga.sjl.utils;

public final class SjlUtils {

    public static String prepareLogMessage(String message, Throwable t) {
        StringBuilder sb = new StringBuilder(message);
        printError(t, null, sb);
        return sb.toString();
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    private static void printError(Throwable t, String header, StringBuilder sb) {
        if (t == null) {
            return;
        }
        String nl = System.getProperty("line.separator");
        if (!isBlank(header)) {
            sb.append(nl).append(header).append(nl).append(nl);
        }
        for (StackTraceElement element : t.getStackTrace()) {
            printStackTraceElement(element, sb).append(nl);
        }
        Throwable next = t.getCause();
        if (next != null) {
            printError(next, "Caused by " + next.getMessage(), sb);
        }
    }

    private static StringBuilder printStackTraceElement(StackTraceElement ste, StringBuilder sb) {
        sb.append(String.format("%s.%s(%s)", ste.getClassName(), ste.getMethodName(),
                ste.isNativeMethod() ? "Native Method" : String.format("%s%s", ste.getFileName(), ste.getLineNumber() > 0 ? ste.getLineNumber() : "")
        ));
        return sb;
    }
}
