package main

import (
	"context"
	"fmt"
	"github.com/docker/docker/api/types"
	"github.com/docker/docker/api/types/image"
	"github.com/docker/docker/client"
	"github.com/sxydh/mgo-util/tar_utils"
	"io"
	"log"
	"os"
	"time"
)

//goland:noinspection GoUnhandledErrorResult
func main() {
	cli, err := client.NewClientWithOpts()
	if err != nil {
		log.Fatalf("NewClientWithOpts error: %v", err)
	}
	defer cli.Close()
	ctx := context.Background()

	// 构建镜像
	dockerfileReader, err := tar_utils.Path2TarReader("tmp/image_build")
	if err != nil {
		log.Fatalf("Path2TarReader error: %v", err)
	}
	imageBuildResponse, err := cli.ImageBuild(
		ctx,
		dockerfileReader,
		types.ImageBuildOptions{
			Tags:       []string{"image_build_demo:" + time.Now().Format("20060102150405")},
			Context:    dockerfileReader,
			Dockerfile: "Dockerfile",
			Remove:     false})
	if err != nil {
		log.Fatalf("ImageBuild error: %v", err)
	}
	defer imageBuildResponse.Body.Close()
	_, err = io.Copy(os.Stdout, imageBuildResponse.Body)
	if err != nil {
		log.Fatalf("Copy error: %v", err)
	}

	// 镜像列表
	imageList, err := cli.ImageList(ctx, image.ListOptions{
		All: true,
	})
	if err != nil {
		log.Fatalf("ImageList error: %v", err)
	}
	for _, img := range imageList {
		log.Printf("ID: %v\n", fmt.Sprint(img.ID))
		for _, tag := range img.RepoTags {
			log.Printf("Tag: %v\n", tag)
		}
	}
}
