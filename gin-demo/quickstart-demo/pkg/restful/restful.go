package restful

import (
	"net/http"
	"quickstart-demo/pkg/e"

	"github.com/gin-gonic/gin"
)

type Response struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
}

func Result(c *gin.Context, httpCode int, bizCode int, msg string, data any) {
	c.JSON(httpCode, Response{
		Code:    bizCode,
		Message: msg,
		Data:    data,
	})
}

func Success(c *gin.Context, data any) {
	Result(c, http.StatusOK, 0, "操作成功", data)
}

func Fail(c *gin.Context, err *e.BizError) {
	Result(c, http.StatusOK, err.Code, err.Msg, nil)
}
