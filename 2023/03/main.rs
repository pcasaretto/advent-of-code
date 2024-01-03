use std::io::{self, Read};
use simple_logger::SimpleLogger;
use std::collections::HashMap;
use log::debug;

fn main() {
    SimpleLogger::new().init().unwrap();

    let mut input = String::new();
    io::stdin().read_to_string(&mut input).unwrap();

    let graph = parse_schematic(&input);
    debug!("{:?}", graph);
    // let sum = sum_part_numbers(&grid);
    // println!("{}", sum);

    let gear_ratio_sum = sum_gear_ratios(graph);
    println!("{}", gear_ratio_sum);
}

fn parse_schematic(input: &str) -> (HashMap<(usize, usize), Vec<i32>>) {
    let mut numbers = HashMap::new();

    let grid: Vec<Vec<char>> = input.lines().map(|line| line.chars().collect()).collect();

    let mut visited = vec![vec![false; grid[0].len()]; grid.len()];

    for i in 0..grid.len() {
        for j in 0..grid[0].len() {
            if visited[i][j] {
                continue;
            }
            if grid[i][j].is_digit(10) {
                if let Some((number, gear_coord)) = extract_number(i, j, &grid, &mut visited) {
                    numbers.entry(gear_coord).or_insert(Vec::new()).push(number);
                }
            }
        }
    }
    numbers
}

fn extract_number(i: usize, j: usize, grid: &[Vec<char>], visited: &mut Vec<Vec<bool>>) -> Option<(i32, (usize, usize))> {
    let mut number = 0;
    let mut k = 0;

    while k + j < grid[0].len() && grid[i][k + j].is_digit(10) {
        number = number * 10 + grid[i][k+j].to_digit(10).unwrap() as i32;
        visited[i][k+j] = true;
        k += 1;
    }

    if let Some(gear_coord) = is_adjacent_to_gear(i, j, k, grid) {
        debug!("{} is adjacent to gear at {:?}", number, gear_coord);
        Some((number, gear_coord))
    } else {
        None
    }
}

fn is_adjacent_to_gear(i: usize, j: usize, x: usize, grid: &[Vec<char>]) -> Option<(usize, usize)> {

    let row_min = if i == 0 { 0 } else { i - 1 };
    let row_max = if i == grid.len() - 1 { i } else { i + 1 };
    let col_min = if j == 0 { 0 } else { j - 1 };
    let col_max = (j + x).min(grid[0].len() - 1);

    for x in row_min..=row_max {
        for y in col_min..=col_max {
            if grid[x][y] == '*' {
                return Some((x, y));
            }
        }
    }
    None
}

fn is_symbol(c: char) -> bool {
    c != '.' && c.is_ascii_punctuation()
}

fn sum_gear_ratios(graph: HashMap<(usize, usize), Vec<i32>>) -> i32 {
    let mut sum = 0;

    for (_, numbers) in graph.iter() {
        if numbers.len() == 2 {
            sum += numbers[0] * numbers[1];
        }
    }

    sum
}