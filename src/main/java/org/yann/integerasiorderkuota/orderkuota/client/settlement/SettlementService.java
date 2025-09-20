package org.yann.integerasiorderkuota.orderkuota.client.settlement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.yann.integerasiorderkuota.orderkuota.entity.User;
import org.yann.integerasiorderkuota.orderkuota.service.UserService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

	private static final Logger log = LoggerFactory.getLogger(SettlementService.class);
	private final RestTemplate restTemplateBuilder = new RestTemplate();
	private final UserService userService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// Constants to avoid repetition
	private static final String PHONE_ANDROID_VERSION = "13";
	private static final String APP_VERSION_CODE = "250819";
	private static final String APP_VERSION_NAME = "25.09.18";
	private static final String UI_MODE = "dark";
	private static final String APP_REG_ID = "cW7nrZuwSTmHMUq38nsYDt:APA91bHtuZh8Rtb4tu4-QkspgkW8WbHF2ZLMHqNesxgpdIDj502JW927xzpwbKPeBt11SYvkFHtHKCVM3rhUkjCku4g5Bm0CD76ACG5ABGy-JfVXdMi09sU";
	private static final String PHONE_UUID = "cW7nrZuwSTmHMUq38nsYDt";
	private static final String USER_AGENT = "okhttp/4.12.0";
	private static final String API_BASE_URL = "https://app.orderkuota.com/api/v2/qris/mutasi/";

	public SettlementDTO getSettlement(String uuid) {
		return getAllSettlement(uuid, "1");
	}

	public SettlementDTO getAllSettlement(String uuid, String pages) {
		User user = userService.getUserDetailsByUUID(uuid);
		if (user == null) {
			return null;
		}

		String timestamp = String.valueOf(System.currentTimeMillis());
		MultiValueMap<String, String> formData = buildFormData(user, pages, timestamp);
		HttpHeaders headers = buildHeaders(timestamp, formData, user.getToken());

		return executeSettlementRequest(user.getToken(), formData, headers);
	}

	public SettlementDTO getSettlementAllHistory(String uuid) {
		SettlementDTO dto = getAllSettlement(uuid, "1");
		if (dto == null) {
			return null;
		}

		List<SettlementDTO.QrisTransaction> allTransactions = new ArrayList<>(dto.getQrisHistory().getResults());
		int totalPages = dto.getQrisHistory().getPages();

		if (totalPages > 1) {
			List<SettlementDTO.QrisTransaction> additionalTransactions = fetchRemainingPages(uuid, totalPages);
			allTransactions.addAll(additionalTransactions);
		}

		allTransactions = allTransactions.stream()
				.sorted(Comparator.comparingLong(SettlementDTO.QrisTransaction::getId).reversed())
				.collect(Collectors.toList());

		dto.getQrisHistory().setResults(allTransactions);
		return dto;
	}

	private MultiValueMap<String, String> buildFormData(User user, String page, String timestamp) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("requests[qris_history][keterangan]", "");
		formData.add("requests[qris_history][jumlah]", "");
		formData.add("phone_android_version", PHONE_ANDROID_VERSION);
		formData.add("app_version_code", APP_VERSION_CODE);
		formData.add("auth_username", user.getUsername());
		formData.add("requests[qris_history][page]", page);
		formData.add("auth_token", user.getToken());
		formData.add("timestamp", timestamp);
		formData.add("app_version_name", APP_VERSION_NAME);
		formData.add("ui_mode", UI_MODE);
		formData.add("requests[qris_history][dari_tanggal]", "");
		formData.add("requests[0]", "account");
		formData.add("requests[qris_history][ke_tanggal]", "");
		formData.add("app_reg_id", APP_REG_ID);
		formData.add("phone_uuid", PHONE_UUID);
		return formData;
	}

	private HttpHeaders buildHeaders(String timestamp, MultiValueMap<String, String> formData, String token) {
		String forCalculate = buildBody(formData.toSingleValueMap());
		String signature = generateSignature(forCalculate, timestamp, token);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("User-Agent", USER_AGENT);
		headers.set("timestamp", timestamp);
		headers.set("signature", signature);
		return headers;
	}

	private SettlementDTO executeSettlementRequest(String token, MultiValueMap<String, String> formData, HttpHeaders headers) {
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
		String url = API_BASE_URL + token.split(":")[0];

		try {
			ResponseEntity<String> response = restTemplateBuilder.exchange(
					url,
					HttpMethod.POST,
					request,
					String.class
			);

			log.info("Response: {}", response.getBody());
			return objectMapper.readValue(response.getBody(), SettlementDTO.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to parse response", e);
		}
	}

	private List<SettlementDTO.QrisTransaction> fetchRemainingPages(String uuid, int totalPages) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		List<CompletableFuture<List<SettlementDTO.QrisTransaction>>> futures = new ArrayList<>();

		for (int i = 2; i <= totalPages; i++) {
			final int page = i;
			CompletableFuture<List<SettlementDTO.QrisTransaction>> future = CompletableFuture.supplyAsync(() -> {
				SettlementDTO pageResult = getAllSettlement(uuid, String.valueOf(page));
				return pageResult != null ? pageResult.getQrisHistory().getResults() : new ArrayList<>();
			}, executor);
			futures.add(future);
		}

		List<SettlementDTO.QrisTransaction> allResults = new ArrayList<>();
		for (CompletableFuture<List<SettlementDTO.QrisTransaction>> future : futures) {
			try {
				List<SettlementDTO.QrisTransaction> result = future.get();
				allResults.addAll(result);
			} catch (InterruptedException | ExecutionException e) {
				log.error("Error fetching page results", e);
			}
		}

		executor.shutdown();
		return allResults;
	}

	private String buildBody(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (!sb.isEmpty()) sb.append("&");
			sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
			sb.append("=");
			sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
		}
		return sb.toString();
	}

	private static String generateSignature(String body, String timestamp, String secret) {
		try {
			String payload = body + timestamp;
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			sha256_HMAC.init(secretKey);
			byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));

			Formatter formatter = new Formatter();
			for (byte b : hash) {
				formatter.format("%02x", b);
			}
			return formatter.toString();
		} catch (Exception e) {
			throw new RuntimeException("Error generate signature", e);
		}
	}
}