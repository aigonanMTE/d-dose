package com.example

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

object Ddose : ModInitializer {
	private val logger = LoggerFactory.getLogger("d-dose")

	override fun onInitialize() {
		// `/test` 명령어 등록 (기본 실행)
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			dispatcher.register(CommandManager.literal("test").executes { context ->
				val player = context.source.player ?: return@executes 0
				executeAirportCommands(player)
				1
			})
		}

		// 서버 메시지 감지 이벤트 (남은 시간 추출)
		ServerMessageEvents.CHAT_MESSAGE.register { message, player, _ ->
			extractAndSendRemainingTime(player, message.toString())
		}
	}

	// 여러 공항 명령어 실행
	private fun executeAirportCommands(player: ServerPlayerEntity) {
		val destinations = listOf("철광산", "메사광산", "페루광산")
		for (destination in destinations) {
			executeCommand(player, "/공항 $destination")
		}
	}

	private fun executeCommand(player: ServerPlayerEntity, command: String) {
		val commandManager = player.server.commandManager
		player.server.execute {
			commandManager.executeWithPrefix(player.commandSource, command)
		}
	}

	private fun extractAndSendRemainingTime(player: ServerPlayerEntity, message: String) {
		val pattern = Pattern.compile("남은시간: (\\d+분 \\d+초|\\d+초)")
		val matcher = pattern.matcher(message)

		if (matcher.find()) {
			val remainingTime = matcher.group(1) // 남은 시간만 추출
			player.server.execute {
				player.server.playerManager.broadcast(Text.literal("남은 시간: $remainingTime"), false)
			}
		}
	}
}
