package hello.itemservice.web.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

	private final ItemRepository itemRepository;

	@GetMapping
	public String items(Model model) {
		List<Item> items = itemRepository.findAll();
		model.addAttribute("items", items);
		return "validation/v2/items";
	}

	@GetMapping("/{itemId}")
	public String item(@PathVariable long itemId, Model model) {
		Item item = itemRepository.findById(itemId);
		model.addAttribute("item", item);
		return "validation/v2/item";
	}

	@GetMapping("/add")
	public String addForm(Model model) {
		model.addAttribute("item", new Item());
		return "validation/v2/addForm";
	}

	@PostMapping("/add")
	public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

		Map<String, String> errors = getFormErrors(item);

		//검증에 실패하면 다시 입력 폼으로
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
			log.info("errors = {}", errors);
			log.info("model = {}", model);
			return "validation/v2/addForm";
		}

		//검증에 성공한 로직

		Item savedItem = itemRepository.save(item);
		redirectAttributes.addAttribute("itemId", savedItem.getId());
		redirectAttributes.addAttribute("status", true);
		return "redirect:/validation/v2/items/{itemId}";
	}

	private Map<String, String> getFormErrors(Item item) {
		//검증 오류 결과 보관
		Map<String, String> errors = new HashMap<>();

		if (!StringUtils.hasText(item.getItemName())) {
			errors.put("itemName", "상품 이름은 필수입니다.");
		}
		if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
			errors.put("price", "가격은 1,000~1,000,000까지 허용합니다.");
		}
		if (item.getQuantity() == null || item.getQuantity() >= 9999) {
			errors.put("quantity", "수량은 최대 9,999 까지 허용합니다.");
		}

		//특정 필드가 아닌 복합 룰 검증
		if (item.getPrice() != null && item.getQuantity() != null) {
			int resultPrice = item.getPrice() * item.getQuantity();
			if (resultPrice < 10000) {
				errors.put("globalError", "가격 * 수량의 합은 10000이상이어야 합니다. 현재 값 = " + resultPrice);
			}
		}
		return errors;
	}

	@GetMapping("/{itemId}/edit")
	public String editForm(@PathVariable Long itemId, Model model) {
		Item item = itemRepository.findById(itemId);
		model.addAttribute("item", item);
		return "validation/v2/editForm";
	}

	@PostMapping("/{itemId}/edit")
	public String edit(@PathVariable Long itemId, @ModelAttribute Item item, Model model) {
		Map<String, String> errors = getFormErrors(item);

		//검증에 실패하면 다시 입력 폼으로
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
			log.info("{}", errors);
			log.info("model = {}", model);
			return "validation/v2/editForm";
		}

		itemRepository.update(itemId, item);
		return "redirect:/validation/v2/items/{itemId}";
	}

}

