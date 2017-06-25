package main 

import (
    "os"
    "fmt"
    "net"
    "bufio"
    "strings"
    "path/filepath"
    //"os/exec"
    //"bytes"
)


func main() {
	/* TODO 
	if os.Args[1] == "-startServer" {
		//cmd := exec.Command("java","-jar","./wurstscript/wurstscript.jar","--about")
		cmd := exec.Command("java","-jar","C:\\work\\WurstScript\\Wurstpack\\wurstscript\\wurstscript.jar","--about")
		var out bytes.Buffer
		cmd.Stdout = &out
		runErr := cmd.Run()
		if runErr != nil {
			fmt.Println("Could not start wurst server ...")
			os.Exit(1)
		}
		fmt.Printf("output: %q\n", out.String())
		return
	}
	*/
	
    conn, err := net.Dial("tcp", "localhost:27425")
	if err != nil {
		 fmt.Println("Error connecting to Wurst Server...")
		 os.Exit(1)
	}
	
	// send arguments:
	for _, arg := range os.Args[1:] {
		if strings.HasPrefix(arg, "-") {
			fmt.Fprintln(conn, arg)
		} else {
			absolutePath, err := filepath.Abs(arg)
			if err != nil {
				fmt.Println("Could not get absolute path for ", arg)
				os.Exit(7)
			}
			fmt.Fprintln(conn, absolutePath)
		}
	}
	// send finished sign
	fmt.Fprintf(conn, "<<<<\n")
	
    status, err := bufio.NewReader(conn).ReadString('\n')
    if err != nil {
		 fmt.Println("Could not read from server Server...")
		 os.Exit(2)
		 return
	}
    fmt.Print("Status = '", status, "'")
    err = conn.Close()
    if err != nil {
    	fmt.Println("Could not close connection...")
    	os.Exit(3)
    }
    if !strings.HasPrefix(status, "ok") {
    	os.Exit(4)
    }
    
}

