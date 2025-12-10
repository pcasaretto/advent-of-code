# Day 9: Movie Theater Tiles

## Problem Summary

A movie theater has a polygon-shaped valid seating area defined by red corner tiles. The polygon is rectilinear (all edges are axis-aligned). Green tiles fill the interior and edges between red tiles.

**Part 1**: Find the largest rectangle where both corners are red tiles (any two red tiles).

**Part 2**: Find the largest rectangle where both corners are red tiles AND the entire rectangle lies within the valid area (only red and green tiles).

## Solution Approach

### Part 1: Brute Force with Transducers

Simple O(n²) approach - check all pairs of red tiles and compute the rectangle area.

```clojure
(defn rect-area [[x1 y1] [x2 y2]]
  (* (inc (abs (- x2 x1))) (inc (abs (- y2 y1)))))
```

Key insight: Area includes both endpoints (tiles, not gaps), so we need `(inc ...)` on both dimensions.

Used `eduction` for memory-efficient streaming over pairs without materializing the full list.

### Part 2: Coordinate Compression + Ray Casting + Prefix Sums

The coordinate space is huge (~100K range) but only 248 unique x/y values from the red tiles. This enables coordinate compression.

#### Step 1: Coordinate Compression

Map the ~100K coordinate range to 247 compressed cells in each dimension. Each cell represents the region between consecutive unique coordinates.

#### Step 2: Ray Casting for Point-in-Polygon

For each compressed cell, determine if it's inside the polygon using ray casting:
- Cast a horizontal ray rightward from the cell center
- Count how many vertical polygon edges it crosses
- Odd count = inside, even count = outside

Optimization: Pre-extract only vertical edges, sorted by x-coordinate.

#### Step 3: 2D Prefix Sums

Build a prefix sum array over the valid/invalid grid. This allows O(1) rectangle validity queries:
- A rectangle is valid if all its cells are inside the polygon
- Using prefix sums: `valid_count = prefix[x2][y2] - prefix[x1][y2] - prefix[x2][y1] + prefix[x1][y1]`
- Rectangle is valid when `valid_count == total_cells`

#### Step 4: Parallel Grid Construction

Use `pmap` to build grid rows in parallel, distributing ray casting across CPU cores.

## Performance

| Phase | Time |
|-------|------|
| Part 1 | ~55ms |
| Part 2 | ~1.7s |

Part 2 optimizations reduced runtime from 21s to 1.7s (~12x speedup):
1. Vertical edge pre-extraction and sorting
2. Parallel grid construction with `pmap`
3. Precomputed cell centers
4. O(1) rectangle queries via prefix sums

## Complexity

- **Part 1**: O(n²) where n = number of red tiles
- **Part 2**: O(k² × e) for grid construction + O(n²) for pair checking
  - k = number of unique coordinates (~248)
  - e = number of vertical edges (~248)
  - Grid construction dominates but is parallelized
