package epfl.dataset;

import java.util.Optional;

public record EPFLCaract(
        Optional<String> orientationBA,
        Optional<String> orientationMA,
        Optional<String> specializationMA,
        Optional<String> option,
        Optional<String> minor,
        Optional<Exchange> typeExchange,
        boolean here) {
}
