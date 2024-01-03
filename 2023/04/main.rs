use log::debug;
use simple_logger::SimpleLogger;
use std::collections::HashSet;
use std::io::{self, Read};

fn main() {
    SimpleLogger::new().init().unwrap();
    let mut input = String::new();
    io::stdin().read_to_string(&mut input).unwrap();

    let cards = parse_cards(&input);
    let total_points = calculate_total_points(cards);
    println!("{}", total_points);
}

fn parse_cards(input: &str) -> Vec<(HashSet<i32>, Vec<i32>)> {
    input
        .lines()
        .map(|line| {
            let parts: Vec<&str> = line.split(": ").collect();
            let numbers: Vec<&str> = parts[1].split(" | ").collect();
            let winning_numbers = numbers[0]
                .split_whitespace()
                .map(|n| n.parse().unwrap())
                .collect();
            let player_numbers = numbers[1]
                .split_whitespace()
                .map(|n| n.parse().unwrap())
                .collect();
            (winning_numbers, player_numbers)
        })
        .collect()
}

fn calculate_total_points(cards: Vec<(HashSet<i32>, Vec<i32>)>) -> i32 {
    cards
        .into_iter()
        .map(|(winning, player)| {
            let mut points = 0;
            for number in player {
                if winning.contains(&number) {
                    debug!("{} is a winning number", number);
                    points += 1;
                }
            }
            if points > 0 {
                2_i32.pow(points - 1)
            } else {
                0
            }
        })
        .sum()
}
