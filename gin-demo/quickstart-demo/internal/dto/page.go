package dto

type PageRequest struct {
	PageSize  int    `form:"pageSize,default=10"`
	SortBy    string `form:"sortBy,default=id"`
	SortOrder string `form:"sortOrder,default=desc"`
}

type PageResponse struct {
	Total int64       `json:"total"`
	List  interface{} `json:"list"`
}
