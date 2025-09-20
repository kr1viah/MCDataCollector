package kr1v.dataCollector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DataCollectorClient implements ClientModInitializer {
	public static boolean isInPoF;
	public static boolean shouldStartNewGame = true;

	public static Data data = new Data();
	public static final Path DATA_PATH = Paths.get("config", "data", "pof-data.json");

	@Override
	public void onInitializeClient() {
		try {
			data = Data.load(DATA_PATH);
		} catch (IOException e) {
			e.printStackTrace();
			data = new Data();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				data.save(DATA_PATH);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}

	public static class Data {
		public List<Game> PoFListOfGames = new ArrayList<>();

		public synchronized void save(Path path) throws IOException {
			Path dir = path.getParent();
			if (dir == null) dir = Paths.get(".");
			Files.createDirectories(dir);

			// timestamp format: 20250919_204530 (yyyyMMdd_HHmmss)
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
			String timestamp = LocalDateTime.now().format(fmt);
			String filename = "pof-data-" + timestamp + ".json";
			Path target = dir.resolve(filename);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
				gson.toJson(this, writer);
			}

			final String prefix = "pof-data-";
			final String suffix = ".json";
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, entry -> {
				String n = entry.getFileName().toString();
				return n.startsWith(prefix) && n.endsWith(suffix);
			})) {
				List<Path> files = new ArrayList<>();
				for (Path p : ds) files.add(p);

				// If more than 20, sort by last modified time ascending (oldest first) and delete extras
				if (files.size() > 20) {
					List<Path> sorted = files.stream()
						.sorted(Comparator.comparingLong(p -> {
							try {
								FileTime ft = Files.getLastModifiedTime(p);
								return ft.toMillis();
							} catch (IOException e) {
								// If we can't read lastModifiedTime, treat as very new to avoid accidental deletion
								return Long.MAX_VALUE;
							}
						}))
						.toList();

					int toDelete = sorted.size() - 20;
					for (int i = 0; i < toDelete; i++) {
						try {
							Files.deleteIfExists(sorted.get(i));
						} catch (IOException ignored) {
							// If deletion fails, continue with the rest (can't do much here)
						}
					}
				}
			}
		}

		public static Data load(Path path) throws IOException {
			Path dir = path.getParent();
			if (dir == null) dir = Paths.get(".");

			if (!Files.exists(dir) || !Files.isDirectory(dir)) {
				return new Data();
			}

			// collect files named pof-data-*.json
			final String prefix = "pof-data-";
			final String suffix = ".json";
			List<Path> candidates = new ArrayList<>();
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, entry -> {
				String n = entry.getFileName().toString();
				return n.startsWith(prefix) && n.endsWith(suffix) && Files.isRegularFile(entry);
			})) {
				for (Path p : ds) candidates.add(p);
			}

			if (candidates.isEmpty()) {
				// no saved snapshots found
				return new Data();
			}

			// filenames are like pof-data-YYYYMMDD_HHMMSS.json which sort lexicographically by time;
			// sort descending so newest is first
			candidates.sort(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed());

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			// try files from newest to oldest, return first successfully parsed Data
			for (Path candidate : candidates) {
				try (Reader reader = Files.newBufferedReader(candidate, StandardCharsets.UTF_8)) {
					Data d = gson.fromJson(reader, Data.class);
					if (d != null) {
						return d;
					}
				} catch (Exception ex) {
					// print and continue to try older files
					ex.printStackTrace();
				}
			}

			// nothing worked
			return new Data();
		}
	}
}
