import 'package:flutter/material.dart';
import '../../viewmodels/feed_viewmodel/feed_viewmodel.dart';

/// Main feed screen that displays a grid of lost items reported by users.
/// Currently, items are retrieved from the local viewmodel (not yet connected to Supabase).
/// Future integration will dynamically fetch data from the backend.
class FeedPage extends StatelessWidget {
  final FeedViewModel viewModel = FeedViewModel();

  FeedPage({super.key});

  @override
  Widget build(BuildContext context) {
    // Retrieve list of lost items 
    final items = viewModel.getLostItems();

    return Scaffold(
      backgroundColor: const Color(0xFFB6D2D2),
      appBar: AppBar(
        backgroundColor: const Color(0xFF4E919D),
        elevation: 0,
        title: const Text(
          "Goatfound",
          style: TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 20,
            color: Color(0xFF2D3A3A),
          ),
        ),
        actions: [
          // Action button to allow users to report a lost item
          Padding(
            padding: const EdgeInsets.only(right: 12.0),
            child: ElevatedButton(
              onPressed: () {
                // Navigate to lost item report screen
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFE49957),
                foregroundColor: const Color(0xFF2D3A3A),
                textStyle: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                ),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                  side: const BorderSide(
                    color: Color(0xFF714E1E),
                    width: 2,
                  ),
                ),
              ),
              child: const Text("Report a lost item"),
            ),
          ),
        ],
      ),

      // Page body with search input and item list
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header text
            const Text(
              "Lost Something?",
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: Color(0xFF2D3A3A),
              ),
            ),
            const SizedBox(height: 4),
            const Text(
              "Search for your item below or report a lost item",
              style: TextStyle(
                fontSize: 14,
                color: Color(0xFF2D3A3A),
              ),
            ),
            const SizedBox(height: 12),

            // Search bar (non-functional for now)
            TextField(
              decoration: InputDecoration(
                hintText: "What did you lose?",
                prefixIcon: const Icon(Icons.search),
                filled: true,
                fillColor: const Color(0xFF6DAEAE),
                hintStyle: const TextStyle(color: Color(0xFF2D3A3A)),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(8),
                  borderSide: BorderSide.none,
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Display either a message or the grid of items
            Expanded(
              child: items.isEmpty
                  ? const Center(
                      child: Text(
                        "No lost items reported yet.",
                        style: TextStyle(color: Color(0xFF2D3A3A)),
                      ),
                    )
                  : GridView.count(
                      crossAxisCount: 2,
                      mainAxisSpacing: 16,
                      crossAxisSpacing: 16,
                      children: items.map((item) {
                        return Container(
                          decoration: BoxDecoration(
                            color: const Color(0xFF6DAEAE),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              // Placeholder image
                              Image.asset(
                                item.imagePath,
                                height: 70,
                              ),
                              const SizedBox(height: 8),
                              Text(
                                item.name,
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: Color(0xFF2D3A3A),
                                ),
                              ),
                            ],
                          ),
                        );
                      }).toList(),
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
