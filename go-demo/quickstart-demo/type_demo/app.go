package type_demo

import (
	"fmt"
	"unsafe"
)

//goland:noinspection GoBoolExpressions
func TypeDemo() {
	var (
		/* byte */
		byteVar = byte('A')

		/* bool */
		boolVar bool = true

		/* int int8 int16 int32 int64 */
		intVar   int   = 42
		int8Var  int8  = 127
		int16Var int16 = 32767
		int32Var int32 = 2147483647
		int64Var int64 = 9223372036854775807

		/* uint uint8 uint16 uint32 uint64 */
		uintVar   uint   = 42
		uint8Var  uint8  = 255
		uint16Var uint16 = 65535
		uint32Var uint32 = 4294967295
		uint64Var uint64 = 18446744073709551615

		/* float32 float64 */
		float32Var float32 = 3.14159
		float64Var float64 = 2.71828

		/* 复数类型
		 * complex64 complex128
		 */
		complex64Var  complex64  = 3 + 4i
		complex128Var complex128 = 3.14 + 2.71i

		/* string */
		stringVar string = "Hello, World!"
	)

	fmt.Printf("byte: %d bytes\n", unsafe.Sizeof(byteVar))

	fmt.Printf("bool: %d bytes\n", unsafe.Sizeof(boolVar))

	fmt.Printf("int: %d bytes\n", unsafe.Sizeof(intVar))
	fmt.Printf("int8: %d bytes\n", unsafe.Sizeof(int8Var))
	fmt.Printf("int16: %d bytes\n", unsafe.Sizeof(int16Var))
	fmt.Printf("int32: %d bytes\n", unsafe.Sizeof(int32Var))
	fmt.Printf("int64: %d bytes\n", unsafe.Sizeof(int64Var))

	fmt.Printf("uint: %d bytes\n", unsafe.Sizeof(uintVar))
	fmt.Printf("uint8: %d bytes\n", unsafe.Sizeof(uint8Var))
	fmt.Printf("uint16: %d bytes\n", unsafe.Sizeof(uint16Var))
	fmt.Printf("uint32: %d bytes\n", unsafe.Sizeof(uint32Var))
	fmt.Printf("uint64: %d bytes\n", unsafe.Sizeof(uint64Var))

	fmt.Printf("float32: %d bytes\n", unsafe.Sizeof(float32Var))
	fmt.Printf("float64: %d bytes\n", unsafe.Sizeof(float64Var))

	fmt.Printf("complex64: %d bytes\n", unsafe.Sizeof(complex64Var))
	fmt.Printf("complex128: %d bytes\n", unsafe.Sizeof(complex128Var))

	fmt.Printf("string: %d bytes\n", unsafe.Sizeof(stringVar))
}
