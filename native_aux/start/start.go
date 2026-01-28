package main

import (
    "os"
    "os/exec"

    "bufio"
    "io"

    "path/filepath"
    "strings"
    "fmt"
    "log"
)

func reteller(from io.ReadCloser, to io.Writer)  {
    reader:= bufio.NewReader(from)
    for {
        if line, _, err:= reader.ReadLine(); err == nil {
            fmt.Fprintf(to, "%s\n", line)
        } else {
            break
        }
    }
    from.Close()
}

func execPath() (string, error) {
    if exe, err:= os.Executable(); err != nil {
        return exe, err
    } else {
        return filepath.EvalSymlinks(exe)
    }
}

func startWait(cmd *exec.Cmd) error {
    if stdout, err:= cmd.StdoutPipe(); err == nil {
    if stderr, err:= cmd.StderrPipe(); err == nil {
    if err:= cmd.Start(); err == nil {
        go reteller(stdout, os.Stdout)
        go reteller(stderr, os.Stderr)
    } else { return err }
    } else { return err }
    } else { return err }

    return cmd.Wait()
}

func main() {
    argLen:= len(os.Args)
    if argLen <= 1 {
        return
    }

    progName:= os.Args[1]
    progArgs:= os.Args[2:argLen]

    startPath, err:= execPath()
    if err != nil { log.Fatal(err) }
    progArgs= append(progArgs, "--start-path", startPath)

    if strings.ToLower(filepath.Ext(progName)) == ".jar" {
        progArgs= append([]string{"-jar", progName}, progArgs...)
        progName= "java"
    }

    cmd:= exec.Command(progName, progArgs...)
    if err:= startWait(cmd); err != nil {
        log.Fatal(err)
    }
}

