package configuration

import (
	"os"
	"path/filepath"
	"testing"

	"github.com/magiconair/properties/assert"
)

func TestPropertiesConfiguration(t *testing.T) {
	cd, _ := os.Getwd()
	res, _ := ReadConfiguration(filepath.Join(cd, "test_data", "simple.properties"))
	assert.Equal(t, res.JavaHome, "/javaHome", "Java Home differs")
	assert.Equal(t, res.LibFolder, "/libFolder", "Lib folder differs")
	assert.Equal(t, res.TempFolder, "/tempFolder", "Temp folder differs")
	assert.Equal(t, res.Args[0], "arg0", "Arg 0 differs")
	assert.Equal(t, res.Args[1], "arg1", "Arg 1 differs")
	assert.Equal(t, len(res.Args), 2, "Length differs")
}

func TestYamlConfiguration(t *testing.T) {
	cd, _ := os.Getwd()
	res, _ := ReadConfiguration(filepath.Join(cd, "test_data", "simple.yml"))
	assert.Equal(t, res.JavaHome, "/javaHome", "Java Home differs")
	assert.Equal(t, res.LibFolder, "/libFolder", "Lib folder differs")
	assert.Equal(t, res.TempFolder, "/tempFolder", "Temp folder differs")
	assert.Equal(t, res.Args[0], "arg1=value1", "Arg 0 differs")
	assert.Equal(t, res.Args[1], "arg2=Значение", "Arg 1 differs")
	assert.Equal(t, len(res.Args), 2, "Length differs")
}
