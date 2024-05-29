package main

import (
	"context"
	"github.com/docker/docker/api/types/image"
	"github.com/docker/docker/client"
	"log"
)

//goland:noinspection GoUnhandledErrorResult
func main() {
	cli, err := client.NewClientWithOpts(client.WithHost("tcp://192.168.233.129:2375"))
	if err != nil {
		log.Fatalf("NewClientWithOpts error: %v", err)
	}
	defer cli.Close()
	ctx := context.Background()

	// 镜像列表
	imageList, err := cli.ImageList(ctx, image.ListOptions{})
	if err != nil {
		log.Printf("ImageList error: %v", err)
	}
	for _, img := range imageList {
		log.Printf(img.RepoTags[0])
		log.Printf(img.ID)
	}
}
