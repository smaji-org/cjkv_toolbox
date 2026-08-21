package main

import (
    "log"
    "os"
    "golang.org/x/sys/windows/registry"
)

func main() {
    name:= os.Args[1]
    update:= len(os.Args) >= 3
    var path string
    if update {
        path= os.Args[2]
    }

    key, err := registry.OpenKey(registry.CURRENT_USER, `Software\Microsoft\Windows NT\CurrentVersion\Fonts`, registry.SET_VALUE)
    if err != nil {
        log.Fatal(err)
    }
    defer key.Close()

    if update {
        err= key.SetStringValue(name, path)
    } else {
        err= key.DeleteValue(name)
    }
    if err != nil {
        log.Fatal(err)
    }
}

