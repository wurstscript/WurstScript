{
  description = "WurstScript compiler development shell (JDK 25, Lua 5.3)";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = { self, nixpkgs }:
    let
      systems = [ "x86_64-linux" ];
      forAllSystems = f: nixpkgs.lib.genAttrs systems (system: f nixpkgs.legacyPackages.${system});
    in
    {
      devShells = forAllSystems (pkgs:
        let
          # Tests probe lua5.3 / luac5.3 (and the unversioned lua53 / luac53
          # names) before falling back to `lua`. nixpkgs lua5_3 only ships
          # `lua` and `luac`.
          lua53Names = pkgs.runCommand "lua-5.3-names" { } ''
            mkdir -p "$out/bin"
            ln -s ${pkgs.lua5_3}/bin/lua "$out/bin/lua5.3"
            ln -s ${pkgs.lua5_3}/bin/luac "$out/bin/luac5.3"
            ln -s ${pkgs.lua5_3}/bin/lua "$out/bin/lua53"
            ln -s ${pkgs.lua5_3}/bin/luac "$out/bin/luac53"
          '';
          jdk = pkgs.jdk25;
        in
        {
          default = pkgs.mkShell {
            packages = [
              jdk
              pkgs.lua5_3
              lua53Names
              pkgs.git
              pkgs.unzip
              pkgs.which
              pkgs.gawk
              pkgs.findutils
              pkgs.curl
              pkgs.cacert
            ];
            # Same JVM flags as the upstream GitHub workflow. Compact headers
            # are a JDK 25 default-path optimization the compiler's JavaExec
            # tasks already request; exporting them here covers the Gradle
            # test workers too.
            JAVA_TOOL_OPTIONS = "-XX:+UseCompactObjectHeaders -XX:+UseStringDeduplication --enable-native-access=ALL-UNNAMED";
            shellHook = ''
              # Nix's openjdk keeps the `release` file Gradle's toolchain
              # detector requires under lib/openjdk, not at the package root.
              export JAVA_HOME="${jdk}/lib/openjdk"
              export PATH="$JAVA_HOME/bin:$PATH"
            '';
          };
        });
    };
}
