package main

import "C"

import (
	"encoding/json"
	"reflect"
)

func marshalJson(obj interface{}) *C.char {
	res, err := json.Marshal(obj)
	if err != nil {
		panic(err.Error())
	}

	return C.CString(string(res))
}

func marshalString(obj interface{}) *C.char {
	if obj == nil {
		return nil
	}

	switch o := obj.(type) {
	case error:
		return C.CString(o.Error())
	case string:
		return C.CString(o)
	}

	panic("invalid marshal type " + reflect.TypeOf(obj).Name())
}
