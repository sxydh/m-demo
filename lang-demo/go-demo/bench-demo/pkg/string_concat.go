package pkg

import "strings"

func SlowConcat(s string, times int) string {
	var result string
	for i := 0; i < times; i++ {
		result += s
	}
	return result
}

func FastConcat(s string, times int) string {
	var builder strings.Builder
	builder.Grow(len(s) * times)

	for i := 0; i < times; i++ {
		builder.WriteString(s)
	}
	return builder.String()
}
