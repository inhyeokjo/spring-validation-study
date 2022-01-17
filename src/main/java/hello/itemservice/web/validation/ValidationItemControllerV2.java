package hello.itemservice.web.validation;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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
	private final ItemValidator itemValidator;

	@InitBinder
	public void init(WebDataBinder dataBinder) {
		dataBinder.addValidators(itemValidator);
	}

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
	public String addItemV1(@Validated @ModelAttribute Item item, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		if (itemValidator.supports(Item.class)) {
			itemValidator.validate(item, bindingResult);
		}
		//검증에 실패하면 다시 입력 폼으로
		if (bindingResult.hasErrors()) {
			log.info("bindingResult = {}", bindingResult);
			return "validation/v2/addForm";
		}

		//검증에 성공한 로직
		Item savedItem = itemRepository.save(item);
		redirectAttributes.addAttribute("itemId", savedItem.getId());
		redirectAttributes.addAttribute("status", true);
		return "redirect:/validation/v2/items/{itemId}";
	}

	@GetMapping("/{itemId}/edit")
	public String editForm(@PathVariable Long itemId, Model model) {
		Item item = itemRepository.findById(itemId);
		model.addAttribute("item", item);
		return "validation/v2/editForm";
	}

	@PostMapping("/{itemId}/edit")
	public String edit(@PathVariable Long itemId, @ModelAttribute Item item, BindingResult bindingResult, Model model) {
		if (itemValidator.supports(Item.class)) {
			itemValidator.validate(item, bindingResult);
		}
		//검증에 실패하면 다시 입력 폼으로
		if (bindingResult.hasErrors()) {
			log.info("bindingResult = {}", bindingResult);
			return "validation/v2/editForm";
		}

		itemRepository.update(itemId, item);
		return "redirect:/validation/v2/items/{itemId}";
	}
}
