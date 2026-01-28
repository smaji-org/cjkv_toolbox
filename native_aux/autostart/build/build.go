package main

import (
    "fmt"
    "os"
    "os/exec"
)

var defaultOs= []string{"windows"}
var defaultArch= []string{"386", "amd64", "arm", "arm64"}

var additionalTargets= map[string][]string {
}

func mapClone [M ~map[K]V, K comparable, V any](m M) M {
    r:= M {}
    for k, v:= range m {
        r[k]= v
    }
    return r
}

func getTargets() map[string]map[string]bool {
    targets:= map[string]map[string]bool {}

    merge:= func (targets map[string]map[string]bool, os string, newArchs map[string]bool) {
        if archs, ok:= targets[os]; ok {
            for newArch:= range newArchs {
                archs[newArch]= true
            }
        } else {
            targets[os]=  mapClone(newArchs)
        }
    }

    for _, os:= range defaultOs {
        someArch:= map[string]bool {}
        for _, arch:= range defaultArch {
            someArch[arch]= true
        }
        targets[os]= someArch
    }

    for os, archs:= range additionalTargets {
        someArch:= map[string]bool {}
        for _, arch:= range archs {
            someArch[arch]= true
        }
        merge(targets, os, someArch)
    }

    return targets
}

func build(errChan chan error, goos string, goarch string, modDir string, outRoot string) {
    progName:= "go"

    outDir:= outRoot + "/" + goos + "-" + goarch + "/"
    progArgs:= []string {"build", "-C", modDir, "-o", outDir}

    if goos == "windows" {
        progArgs= append(progArgs, "-ldflags", "-H=windowsgui") 
    }

    progArgs= append(progArgs, ".")

    cmd:= exec.Command(progName, progArgs...)
    cmd.Env = append(os.Environ(), "GOOS="+goos, "GOARCH="+goarch)
    out, err:= cmd.CombinedOutput()
    if err != nil {
        errChan <- fmt.Errorf("%s", out)
    } else {
        errChan <- nil
    }
}

func main() {
    modDir:= os.Args[1]
    outDir:= os.Args[2]

    targets:= getTargets()
    buildNum:= 0
    c:= make(chan error)
    for os, archs:= range targets {
        for arch:= range archs {
            go build(c, os, arch, modDir, outDir)
            buildNum+= 1
        }
    }
    for i:= 0; i < buildNum; i++ {
        if err:= <- c; err != nil {
            fmt.Print(err.Error())
        }
    }
}
