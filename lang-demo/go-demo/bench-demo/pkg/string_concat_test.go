package pkg

import (
	"math/rand/v2"
	"os"
	"runtime/pprof"
	"strings"
	"testing"
)

//goland:noinspection GoUnusedGlobalVariable
var result string

func BenchmarkSlowConcat(b *testing.B) {
	rng := rand.New(rand.NewPCG(12345, 67890)) // 种子可固定以保证可复现性
	input := generateRandomString(rng, 10)
	repeatTimes := 1000

	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		result = SlowConcat(input, repeatTimes)
	}
}

func BenchmarkFastConcat(b *testing.B) {
	rng := rand.New(rand.NewPCG(12345, 67890)) // 种子可固定以保证可复现性
	input := generateRandomString(rng, 10)
	repeatTimes := 1000

	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		result = FastConcat(input, repeatTimes)
	}
}

func BenchmarkSlowConcatManual(b *testing.B) {
	// 可选：手动控制 CPU profiling 的开始和结束，避免无关代码开销的干扰。
	// 此部分功能控制的是 _cpu_manual.prof 的采样，与 b.ResetTimer() 并不重复。
	f, err := os.Create("_cpu_manual.prof")
	if err != nil {
		panic(err)
	}
	defer func(f *os.File) {
		_ = f.Close()
	}(f)

	if err := pprof.StartCPUProfile(f); err != nil {
		panic(err)
	}
	defer pprof.StopCPUProfile()

	rng := rand.New(rand.NewPCG(12345, 67890)) // 种子可固定以保证可复现性
	input := generateRandomString(rng, 10)
	repeatTimes := 1000

	// 重置 testing.B 结构体内部的 nsPerOp/mallocsPerOp/bytesPerOp ，确保 benchmark 统计的是纯性能。
	b.ResetTimer()

	for i := 0; i < b.N; i++ {
		result = SlowConcat(input, repeatTimes)
	}
}

func generateRandomString(rng *rand.Rand, n int) string {
	const letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
	sb := strings.Builder{}
	sb.Grow(n)
	for i := 0; i < n; i++ {
		sb.WriteByte(letters[rng.IntN(len(letters))])
	}
	return sb.String()
}
