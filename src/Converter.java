import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Converter {

    // Instale estes dois pacotes primeiro, pois sao dependencias. Pode instalar usando brew
    private static final String FFMPEG = "ffmpeg";          // precisa estar no PATH
    private static final String HEIF_CONVERT = "heif-convert"; // precisa estar no PATH

    public static void main(String[] args) throws IOException {
        Path origem = Paths.get("/Users/youruser/any sub dir");
        Path destino = Paths.get("/Users/bfa/Downloads/temporario");

        Files.walk(origem).forEach(path -> {
            if (Files.isDirectory(path)) return;

            try {
                String nomeArquivo = path.getFileName().toString();
                String ext = getExtensao(nomeArquivo).toLowerCase();

                switch (ext) {
                    case "jpg":
                    case "jpeg":
                    case "mp4":
                        if (Files.exists(destino.resolve(nomeArquivo))) {
                            break;
                        }
                        // Nao converte, apenas copia
                        Path destinoArquivo = destino.resolve(nomeArquivo);
                        Files.copy(path, destinoArquivo, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Copiado: " + nomeArquivo);
                        break;

                    case "heic":
                    case "heif":
                        // converte para JPEG
                        String novoNomeImg = nomeArquivo.replaceAll("(?i)\\.(heic|heif)$", ".jpg");
                        if (Files.exists(destino.resolve(novoNomeImg))) {
                            System.out.println("Arquivo ja existe: " + novoNomeImg);
                            break;
                        }

                        Path destinoImg = destino.resolve(novoNomeImg);
                        runHeifConvert(path.toString(), destinoImg.toString());
                        break;

                    case "mov":
                    case "qt":
                        // converte para MP4
                        String novoNomeVideo = nomeArquivo.replaceAll("(?i)\\.(mov|qt)$", ".mp4");
                        if (Files.exists(destino.resolve(novoNomeVideo))) {
                            System.out.println("Arquivo ja existe: " + novoNomeVideo);
                            break;
                        }
                        Path destinoVideo = destino.resolve(novoNomeVideo);
                        runFFmpeg(path.toString(), destinoVideo.toString());
                        break;
                    default:
                        System.out.println("Ignorado (formato não reconhecido): " + nomeArquivo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String getExtensao(String nome) {
        int i = nome.lastIndexOf('.');
        return (i > 0) ? nome.substring(i + 1) : "";
    }

    private static void runFFmpeg(String origem, String destino) throws IOException, InterruptedException {
        String[] comando = new String[]{FFMPEG, "-i", origem, "-c:v", "libx264", "-crf", "23", "-c:a", "aac", destino};
        ProcessBuilder pb = new ProcessBuilder(comando);
        // pb.inheritIO(); // mostra saída do ffmpeg
        Process processo = pb.start();
        int exitCode = processo.waitFor();

        if (exitCode == 0) {
            System.out.println("Convertido vídeo: " + origem + " -> " + destino);
        } else {
            System.err.println("Erro na conversão de vídeo: " + origem);
        }
    }

    private static void runHeifConvert(String origem, String destino) throws IOException, InterruptedException {
        String[] comando = new String[]{HEIF_CONVERT, origem, destino};
        ProcessBuilder pb = new ProcessBuilder(comando);
        // pb.inheritIO(); // mostra saída do heif-convert
        Process processo = pb.start();
        int exitCode = processo.waitFor();

        if (exitCode == 0) {
            System.out.println("Convertido imagem HEIF: " + origem + " -> " + destino);
        } else {
            System.err.println("Erro na conversão de HEIF: " + origem);
        }
    }
}
