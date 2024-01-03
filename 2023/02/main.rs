use std::io::{self, Read};
use std::collections::HashMap;

fn main() {
    let mut input = String::new();
    io::stdin().read_to_string(&mut input).unwrap();

    let games = parse_input(&input);
    let possible_games_sum = sum_possible_games(&games, 12, 13, 14);
    println!("{}", possible_games_sum);
}

fn parse_input(input: &str) -> HashMap<i32, Vec<HashMap<String, i32>>> {
    let mut games = HashMap::new();

    for line in input.lines() {
        let parts: Vec<&str> = line.split(": ").collect();
        let game_id: i32 = parts[0].split_whitespace().last().unwrap().parse().unwrap();
        let mut draws = Vec::new();

        for draw in parts[1].split("; ") {
            let mut cubes = HashMap::new();
            for cube in draw.split(", ") {
                let parts: Vec<&str> = cube.split_whitespace().collect();
                let count: i32 = parts[0].parse().unwrap();
                let color = parts[1].to_string();
                *cubes.entry(color).or_insert(0) += count;
            }
            draws.push(cubes);
        }

        games.insert(game_id, draws);
    }

    games
}

fn sum_possible_games(games: &HashMap<i32, Vec<HashMap<String, i32>>>, red: i32, green: i32, blue: i32) -> i32 {
    let mut sum = 0;

    for (&game_id, draws) in games {
        if is_game_possible(draws, red, green, blue) {
            sum += game_id;
        }
    }

    sum
}

fn is_game_possible(draws: &Vec<HashMap<String, i32>>, red: i32, green: i32, blue: i32) -> bool {
    for draw in draws {
        if draw.get("red").unwrap_or(&0) > &red
            || draw.get("green").unwrap_or(&0) > &green
            || draw.get("blue").unwrap_or(&0) > &blue {
            return false;
        }
    }
    true
}
