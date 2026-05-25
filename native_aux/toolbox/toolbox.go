package main

import (
    "os"
    "os/exec"

    "bufio"
    "io"

    "path/filepath"
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

func getExecPath() (string, error) {
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

    exePath, err:= getExecPath()
    if err != nil { log.Fatal(err) }
    exeDir:= filepath.Dir(exePath)
    toolboxPath:= filepath.Join(exeDir, "toolbox.jar")

    progName:= "java"
    progArgs:= append([]string{"-jar", toolboxPath}, os.Args[1:argLen]...)

    cmd:= exec.Command(progName, progArgs...)
    if err:= startWait(cmd); err != nil {
        log.Fatal(err)
    }
}

