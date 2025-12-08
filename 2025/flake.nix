{
  description = "Advent of Code 2025 - Clojure";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            clojure
            clojure-lsp
            babashka
            rlwrap
          ];

          shellHook = ''
            echo "Advent of Code 2025 - Clojure environment ready"
            echo "Run solutions with: clojure -M src/solution.clj"
            echo "Or use babashka: bb src/solution.clj"
          '';
        };
      });
}
