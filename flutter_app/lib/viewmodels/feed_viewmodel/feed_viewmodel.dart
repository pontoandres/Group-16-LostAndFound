class LostItem {
  final String name;
  final String imagePath;

  LostItem({required this.name, required this.imagePath});
}

class FeedViewModel {
  
  List<LostItem> getLostItems() {
   
    return [];
  }
}
