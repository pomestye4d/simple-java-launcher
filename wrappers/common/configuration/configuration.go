package configuration

import (
	"fmt"
	"os"
	"strings"

	"github.com/magiconair/properties"
	"gopkg.in/yaml.v3"
)

type Configuration struct {
	Args       []string
	LibFolder  string
	TempFolder string
	JavaHome   string
}

func ReadConfiguration(filename string) (Configuration, error) {
	if strings.HasSuffix(filename, ".properties") {
		p := properties.MustLoadFile(filename, properties.UTF8)
		c := Configuration{}
		for _, k := range p.Keys() {
			value := p.MustGetString(k)
			if strings.HasPrefix(k, "sjl.javaHome") {
				c.JavaHome = value
				continue
			}
			if strings.HasPrefix(k, "sjl.libFolder") {
				c.LibFolder = value
				continue
			}
			if strings.HasPrefix(k, "sjl.tempFolder") {
				c.TempFolder = value
				continue
			}
			if strings.HasPrefix(k, "sjl.args") {
				c.Args = append(c.Args, value)
				continue
			}
		}
		return c, nil
	}
	if strings.HasSuffix(filename, ".yaml") || strings.HasSuffix(filename, ".yml") {
		yfile, err := os.ReadFile(filename)
		if err != nil {
			return Configuration{}, err
		}
		// data := make(map[string]yamlSjl)
		data := make(map[string]map[string]interface{})

		err2 := yaml.Unmarshal(yfile, &data)
		if err2 != nil {
			return Configuration{}, err2
		}
		c := Configuration{}
		sjl := data["sjl"]
		if sjl["javaHome"] != nil {
			c.JavaHome = sjl["javaHome"].(string)
		}
		if sjl["libFolder"] != nil {
			c.LibFolder = sjl["libFolder"].(string)
		}
		if sjl["tempFolder"] != nil {
			c.TempFolder = sjl["tempFolder"].(string)
		}
		c.Args = []string{}

		if sjl["args"] != nil {
			args := sjl["args"].([]interface{})
			for _, arg := range args {
				c.Args = append(c.Args, arg.(string))
			}
		}
		return c, nil
	}
	return Configuration{}, fmt.Errorf("unsupported file name %s", filename)

}
