package main

import (
    "bufio"
    "fmt"
    "os"
    "strconv"
    "unicode"
)

func main() {
    scanner := bufio.NewScanner(os.Stdin)
    sum := 0

    for scanner.Scan() {
        line := scanner.Text()
        firstDigit, lastDigit := findDigits(line)

        if firstDigit != -1 && lastDigit != -1 {
            value, _ := strconv.Atoi(fmt.Sprintf("%d%d", firstDigit, lastDigit))
            sum += value
        }
    }

    if err := scanner.Err(); err != nil {
        fmt.Println("Error reading file:", err)
        return
    }

    fmt.Println("Sum of calibration values:", sum)
}

func findDigits(s string) (int, int) {
    var firstDigit, lastDigit int = -1, -1

    for _, r := range s {
        if unicode.IsDigit(r) {
            digit := int(r - '0')

            if firstDigit == -1 {
                firstDigit = digit
            }

            lastDigit = digit
        }
    }

    return firstDigit, lastDigit
}
