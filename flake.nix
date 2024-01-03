{
  inputs = {
    nixpkgs.url = "nixpkgs/nixos-23.11";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let pkgs = nixpkgs.legacyPackages.${system};
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
          pkgs.rustc
          pkgs.cargo
          pkgs.cargo-watch
          pkgs.rust-analyzer
          pkgs.rustPlatform.rustcSrc
        ]; };
      });
}
