use std::collections::HashMap;

pub fn _fn() {
    pointer_var();
    println!();
    ref_var();
    println!();
    constructed_type_var();
}

/* 指针变量 */
fn pointer_var() {
    println!("pointer var");
    let i = 1;
    // 注意 & 目的不是取地址，而是创建引用，虽然引用本质上是一个操作受限的指针。
    let i_ptr: *const i32 = &i;
    println!("i = {}, i_ptr = {:p}", i, i_ptr);
}

/* 引用变量 */
fn ref_var() {
    println!("ref var");
    let mut map = HashMap::new();
    map.insert("apple", 1);
    map.insert("banana", 2);
    map.insert("cherry", 3);
    // 注意 & 目的不是取地址，而是创建引用，虽然引用本质上是一个操作受限的指针。
    let map_ref = &map;
    println!("map = {:?}, map_ref = {:?}, *map_ref= {:?}", map, map_ref, *map_ref);
}

/* 构造类型变量 */
fn constructed_type_var() {
    println!("constructed type var");
    let mut map = HashMap::new();
    map.insert("apple", 1);
    map.insert("banana", 2);
    map.insert("cherry", 3);
    println!("map = {:?}", map);
}