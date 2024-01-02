{
  inputs = {
    nixpkgs.url = "nixpkgs/nixos-23.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let pkgs = nixpkgs.legacyPackages.${system};
      in {
        devShell = pkgs.mkShell { buildInputs = [
          pkgs.go
          pkgs.gopls
          pkgs.gotools
          pkgs.go-tools
          pkgs.rustc
          pkgs.cargo
          pkgs.cargo-watch
          pkgs.rust-analyzer
          pkgs.rustPlatform.rustcSrc
        ]; };
      });
}
