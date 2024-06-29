package main

import (
	"log"
	"strconv"
	"testing"
)

func TestFs(t *testing.T) {
	tests := []struct {
		name string
	}{
		{
			name: "Normal",
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := Fs()
			log.Printf(strconv.Itoa(got))
			select {}
		})
	}
}
