package e

type BizError struct {
	Code int    `json:"code"`
	Msg  string `json:"msg"`
}

func (bizErr *BizError) Error() string {
	return bizErr.Msg
}

var (
	BadRequestError    = &BizError{Code: 400, Msg: "参数错误"}
	UnauthorizedError  = &BizError{Code: 401, Msg: "认证错误"}
	InternalError      = &BizError{Code: 500, Msg: "内部错误"}
	UserNotExistError  = &BizError{Code: 1000, Msg: "用户不存在"}
	UsernameExistError = &BizError{Code: 1010, Msg: "用户名称已存在"}
	EmailExistError    = &BizError{Code: 1020, Msg: "用户邮箱已存在"}
	LoginError         = &BizError{Code: 1030, Msg: "用户名或密码错误"}
)
