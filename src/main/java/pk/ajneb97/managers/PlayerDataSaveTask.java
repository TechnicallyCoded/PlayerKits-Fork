package pk.ajneb97.managers;

import pk.ajneb97.PlayerKits;

public class PlayerDataSaveTask {

	private PlayerKits plugin;
	private boolean stop;
	public PlayerDataSaveTask(PlayerKits plugin) {
		this.plugin = plugin;
		this.stop = false;
	}
	
	public void end() {
		this.stop = true;
	}
	
	public void start() {
		int timeSeconds = plugin.getConfig().getInt("Config.player_data_save_time");

		plugin.getFoliaLib().getImpl().runTimerAsync((task) -> {
			if (stop) task.cancel();
			else execute();
		}, 0L, 20L * timeSeconds);

	}
	
	public void execute() {
		plugin.getJugadorManager().guardarJugadores();
	}
}
