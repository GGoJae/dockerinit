package com.dockerinit.linux.crawling;

import org.springframework.stereotype.Service;

@Service
public class ManFetchService {

    public ManRaw fetch(String command) {
        String shell = """
            docker run --rm ubuntu:24.04 bash -lc '
              set -e
              export DEBIAN_FRONTEND=noninteractive
              apt-get update >/dev/null
              apt-get -y install man-db manpages >/dev/null
              MANWIDTH=1000 man %s | col -b
            '
            """.formatted(command);

        String text = run(shell);
        return new ManRaw(command, "1", "ubuntu-24.04", "local-man", text);
    }

    private String run(String shell) {
        try {
            Process p = new ProcessBuilder("bash", "-lc", shell)
                    .redirectErrorStream(true)
                    .start();
            String out = new String(p.getInputStream().readAllBytes());
            int code = p.waitFor();
            if (code != 0) throw new IllegalStateException("man exec failed: " + code);

            return out;
        } catch (Exception e) {
            throw new IllegalStateException("man fetch error", e);
        }
    }

    public record ManRaw(String command, String section, String distro, String source, String text) {}
}
