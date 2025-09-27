package kr1v.dataCollector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kr1v.dataCollector.games.LuckyIslandsGame;
import kr1v.dataCollector.games.PoFGame;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

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
	public static GameMode currentGame = null;

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

		ClientCommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess) ->
			commandDispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("show-stats").executes(context -> {
				MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(new PoFStatsScreen()));
				return 0;
			})));
	}

	public static class Data {
		public List<PoFGame> ListOfPoFGames = new ArrayList<>();
		public List<LuckyIslandsGame> ListOfLuckyIslandsGames = new ArrayList<>();

		public synchronized void save(Path path) throws IOException {
			Path dir = path.getParent();
			if (dir == null) dir = Paths.get(".");
			Files.createDirectories(dir);

			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
			String timestamp = LocalDateTime.now().format(fmt);
			String filename = "data-" + timestamp + ".json";
			Path target = dir.resolve(filename);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
				gson.toJson(this, writer);
			}

			final String prefix = "data-";
			final String suffix = ".json";
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, entry -> {
				String n = entry.getFileName().toString();
				return n.startsWith(prefix) && n.endsWith(suffix);
			})) {
				List<Path> files = new ArrayList<>();
				for (Path p : ds) files.add(p);

				if (files.size() > 20) {
					List<Path> sorted = files.stream()
						.sorted(Comparator.comparingLong(p -> {
							try {
								FileTime ft = Files.getLastModifiedTime(p);
								return ft.toMillis();
							} catch (IOException e) {
								return Long.MAX_VALUE;
							}
						}))
						.toList();

					int toDelete = sorted.size() - 20;
					for (int i = 0; i < toDelete; i++) {
						try {
							Files.deleteIfExists(sorted.get(i));
						} catch (IOException ignored) {
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

			final String prefix = "data-";
			final String suffix = ".json";
			List<Path> candidates = new ArrayList<>();
			try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, entry -> {
				String n = entry.getFileName().toString();
				return n.startsWith(prefix) && n.endsWith(suffix) && Files.isRegularFile(entry);
			})) {
				for (Path p : ds) candidates.add(p);
			}

			if (candidates.isEmpty()) {
				return new Data();
			}

			candidates.sort(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed());

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			for (Path candidate : candidates) {
				try (Reader reader = Files.newBufferedReader(candidate, StandardCharsets.UTF_8)) {
					Data d = gson.fromJson(reader, Data.class);
					if (d != null) {
						return d;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			return new Data();
		}
	}
}
