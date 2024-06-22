package goroutine_demo

import (
	"fmt"
	"sync"
	"time"
)

func GoroutineDemo() {
	var wg = sync.WaitGroup{}
	wg.Add(2)
	go func() {
		job()
		wg.Done()
	}()
	go func() {
		job2()
		wg.Done()
	}()
	wg.Wait()
}

func job() {
	for {
		fmt.Println("job")
		time.Sleep(2 * time.Second)
	}
}

func job2() {
	for {
		fmt.Println("job2")
		time.Sleep(2 * time.Second)
	}
}
