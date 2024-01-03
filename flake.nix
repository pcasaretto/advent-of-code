{
  inputs = {
    nixpkgs.url = "nixpkgs/nixos-23.11";
    flake-utils.url = "github:numtide/flake-utils";
    fenix.url = "github:nix-community/fenix";
  };

  outputs = { self, nixpkgs, flake-utils, fenix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
      pkgs = import nixpkgs {
        inherit system; 
        overlays = [fenix.overlays.default]; 
      };
      rustToolchain = let 
          inherit (pkgs.fenix) combine complete targets;
        in combine [
          complete.cargo
          complete.rustc
          complete.rustfmt
          complete.clippy
          complete.rust-analyzer
        ];
      in {
        devShell = pkgs.mkShell {
          buildInputs = [
            pkgs.libiconv
          ];
          nativeBuildInputs = [
          pkgs.go
          pkgs.gopls
          pkgs.gotools
          pkgs.go-tools
          rustToolchain
        ]; };
      });
}
