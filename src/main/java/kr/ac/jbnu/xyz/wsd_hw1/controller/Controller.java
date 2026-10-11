package kr.ac.jbnu.xyz.wsd_hw1.controller;

import kr.ac.jbnu.xyz.wsd_hw1.api.dto.ItemDto;
import kr.ac.jbnu.xyz.wsd_hw1.request.ItemCreateRequest;
import kr.ac.jbnu.xyz.wsd_hw1.request.ItemUpdateRequest;
import kr.ac.jbnu.xyz.wsd_hw1.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/items")
public class Controller {

    private final Map<Long, ItemDto> store = new HashMap<>();
    private long sequence = 1L;

    // ✅ GET: 전체 조회 (200 OK)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemDto>>> getItems() {
        return ResponseEntity.ok(ApiResponse.success(new ArrayList<>(store.values())));
    }

    // ✅ GET: 단건 조회 (200 OK, 404 Not Found)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> getItem(@PathVariable Long id) {
        ItemDto item = store.get(id);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    // ✅ GET: 이름으로 검색 (200 OK, 400 Bad Request)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ItemDto>>> searchItems(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Search keyword is required"));
        }
        List<ItemDto> result = new ArrayList<>();
        for (ItemDto item : store.values()) {
            if (item.getName().contains(name)) {
                result.add(item);
            }
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ✅ POST: 생성 (201 Created, 400 Bad Request)
    @PostMapping
    public ResponseEntity<ApiResponse<ItemDto>> createItem(@RequestBody ItemCreateRequest request) {
        if (request.getName() == null || request.getPrice() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid request data"));
        }
        ItemDto item = new ItemDto();
        item.setId(sequence++);
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        store.put(item.getId(), item);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(item));
    }

    // ✅ POST: 여러 개 생성 (201 Created, 500 Internal Server Error)
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<ItemDto>>> createItems(@RequestBody List<ItemCreateRequest> requests) {
        try {
            List<ItemDto> created = new ArrayList<>();
            for (ItemCreateRequest req : requests) {
                ItemDto item = new ItemDto();
                item.setId(sequence++);
                item.setName(req.getName());
                item.setPrice(req.getPrice());
                store.put(item.getId(), item);
                created.add(item);
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Bulk creation failed"));
        }
    }

    // ✅ PUT: 단건 수정 (200 OK, 404 Not Found)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> updateItem(
            @PathVariable Long id,
            @RequestBody ItemUpdateRequest request
    ) {
        ItemDto item = store.get(id);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found"));
        }
        if (request.getName() != null) item.setName(request.getName());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    // ✅ PUT: 전체 가격 수정 (200 OK, 503 Service Unavailable)
    @PutMapping("/updateAllPrice")
    public ResponseEntity<ApiResponse<List<ItemDto>>> updateAllPrice(@RequestParam Integer price) {
        if (store.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("No items available to update"));
        }
        for (ItemDto item : store.values()) {
            item.setPrice(price);
        }
        return ResponseEntity.ok(ApiResponse.success(new ArrayList<>(store.values())));
    }

    // ✅ DELETE: 단건 삭제 (204 No Content, 404 Not Found)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        ItemDto removed = store.remove(id);
        if (removed == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item not found"));
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ✅ DELETE: 전체 삭제 (204 No Content, 500 Internal Server Error)
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Void>> deleteAllItems() {
        try {
            store.clear();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete all items"));
        }
    }
}
