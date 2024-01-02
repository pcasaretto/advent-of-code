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
    wordsToDigits := map[string]int{
        "one": 1, "two": 2, "three": 3, "four": 4, "five": 5,
        "six": 6, "seven": 7, "eight": 8, "nine": 9,
    }
    partialWords := generatePartialWords(wordsToDigits)

    var firstDigit, lastDigit int = -1, -1
    var currentWord string

    for i := 0; i < len(s); i++ {
		r := rune(s[i])
        if unicode.IsLetter(r) {
            currentWord += string(r)
            if _, exists := partialWords[currentWord]; !exists {
				i -= len(currentWord) - 1 // rewind index to start of current word minus 1
                currentWord = "" // reset current word
            } else if digit, complete := wordsToDigits[currentWord]; complete {
                if firstDigit == -1 {
                    firstDigit = digit
                }
                lastDigit = digit
				i -= len(currentWord) - 1 // rewind index to start of current word minus 1
                currentWord = "" // reset current word
            }
        } else if unicode.IsDigit(r) {
            digit := int(r - '0')
            if firstDigit == -1 {
                firstDigit = digit
            }
            lastDigit = digit
        }
    }

    return firstDigit, lastDigit
}

func generatePartialWords(wordsToDigits map[string]int) map[string]bool {
    partialWords := make(map[string]bool)
    for word := range wordsToDigits {
        for i := 1; i <= len(word); i++ {
            partialWords[word[:i]] = true
        }
    }
    return partialWords
}
