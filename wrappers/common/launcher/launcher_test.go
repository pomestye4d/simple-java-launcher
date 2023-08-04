package launcher

import (
	"fmt"
	"testing"

	"github.com/magiconair/properties/assert"
)

func TestLauncherInitialization(t *testing.T) {
	data, e := InitLauncherData()
	fmt.Println(e)
	assert.Equal(t, e, nil, "There are errors")
	fmt.Println(data.StartCommand)
}
