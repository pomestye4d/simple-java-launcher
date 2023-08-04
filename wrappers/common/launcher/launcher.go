package launcher

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"regexp"
	"runtime"
	"vga/sjl/common/configuration"
	"vga/sjl/common/utils"
)

type LauncherData struct {
	StartCommand string
	TempFolder   string
	PidFileName  string
}

func InitLauncherData() (LauncherData, error) {
	currentDirectory, e := os.Getwd()
	if e != nil {
		return LauncherData{}, e
	}
	workingDirectory := currentDirectory
	wdStr := os.Getenv("sjl.workingDirectory")
	if len(wdStr) != 0 {
		workingDirectory = wdStr
	}
	if !filepath.IsAbs(workingDirectory) {
		workingDirectory = filepath.Join(currentDirectory, workingDirectory)
	}
	if !utils.IsDirectoryExists(workingDirectory) {
		return LauncherData{}, fmt.Errorf("working directory %s does not exist", workingDirectory)
	}
	configFileName := os.Getenv("sjl.configFile")
	if len(configFileName) != 0 {
		if filepath.IsAbs(configFileName) {
			if !utils.IsFileExists(configFileName) {
				return LauncherData{}, fmt.Errorf("config file %s does not exist", configFileName)
			}
		} else {
			configFileName = filepath.Join(workingDirectory, configFileName)
			if !utils.IsFileExists(configFileName) {
				return LauncherData{}, fmt.Errorf("config file %s does not exist", configFileName)
			}
		}
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "config.yml")) {
		configFileName = filepath.Join(workingDirectory, "config.yml")
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "config.yaml")) {
		configFileName = filepath.Join(workingDirectory, "config.yaml")
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "application.yaml")) {
		configFileName = filepath.Join(workingDirectory, "application.yaml")
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "application.yml")) {
		configFileName = filepath.Join(workingDirectory, "application.yml")
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "config", "config.yaml")) {
		configFileName = filepath.Join(workingDirectory, "config", "config.yaml")
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "config", "config.yml")) {
		configFileName = filepath.Join(workingDirectory, "config", "config.yml")
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "config", "application.yaml")) {
		configFileName = filepath.Join(workingDirectory, "config", "application.yaml")
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "config", "application.yml")) {
		configFileName = filepath.Join(workingDirectory, "config", "application.yml")
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "config.properties")) {
		configFileName = filepath.Join(workingDirectory, "config.propertise")
	} else if utils.IsFileExists(filepath.Join(workingDirectory, "config", "config.properties")) {
		configFileName = filepath.Join(workingDirectory, "config", "config.propertise")
	} else {
		return LauncherData{}, errors.New("unable to locate config file")
	}
	config, e := configuration.ReadConfiguration(configFileName)
	if e != nil {
		return LauncherData{}, e
	}
	libFolderName := "lib"
	if len(config.LibFolder) != 0 {
		libFolderName = config.LibFolder
	}
	if !filepath.IsAbs(libFolderName) {
		libFolderName = filepath.Join(workingDirectory, libFolderName)
	}
	if !utils.IsDirectoryExists(libFolderName) {
		return LauncherData{}, fmt.Errorf("lib folder %s does not exist", libFolderName)
	}
	tempFolderName := "temp"
	if len(config.TempFolder) != 0 {
		tempFolderName = config.TempFolder
	}
	if !filepath.IsAbs(tempFolderName) {
		tempFolderName = filepath.Join(workingDirectory, tempFolderName)
	}
	if !utils.IsDirectoryExists(tempFolderName) {
		e := os.Mkdir(tempFolderName, os.ModeDir)
		if e != nil {
			return LauncherData{}, e
		}
	}
	launcherRE := regexp.MustCompile(`sjl[\-\.0-9]*\.jar`)
	files, e := os.ReadDir(libFolderName)
	if e != nil {
		return LauncherData{}, e
	}
	launcherJar := ""
	for _, file := range files {
		if !file.IsDir() && launcherRE.MatchString(file.Name()) {
			launcherJar = filepath.Join(libFolderName, file.Name())
			break
		}
	}
	if !utils.IsFileExists(launcherJar) {
		return LauncherData{}, errors.New("unable to locate launcher")
	}

	javaHome := "jre"
	if len(config.JavaHome) != 0 {
		javaHome = config.JavaHome
	} else if len(os.Getenv("JAVA_HOME")) != 0 {
		javaHome = os.Getenv("JAVA_HOME")
	}

	if !filepath.IsAbs(javaHome) {
		javaHome = filepath.Join(workingDirectory, javaHome)
	}

	if !utils.IsDirectoryExists(javaHome) {
		return LauncherData{}, fmt.Errorf("java home folder %s does not exist", javaHome)
	}
	result := LauncherData{}
	javaExec := "bin/java"
	if runtime.GOOS == "windows" {
		javaExec = "bin\\java.exe"
	}
	result.PidFileName = filepath.Join(tempFolderName, "sjl.pid")
	result.StartCommand = fmt.Sprintf("cd \"%s\" && \"%s\" -cp \"%s\" com.vga.sjl.SjlBoot", workingDirectory, filepath.Join(javaHome, javaExec), launcherJar)
	result.TempFolder = tempFolderName
	return result, nil
}
