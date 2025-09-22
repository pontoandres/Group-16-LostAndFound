import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../theme/app_theme.dart';

class CameraScreen extends StatefulWidget {
  const CameraScreen({super.key});

  @override
  State<CameraScreen> createState() => _CameraScreenState();
}

class _CameraScreenState extends State<CameraScreen> {
  File? _photo;
  bool _taking = false;

  Future<void> _takePhoto() async {
    if (_taking) return;
    _taking = true;
    try {
      final x = await ImagePicker().pickImage(source: ImageSource.camera);
      if (!mounted) return;
      if (x != null) setState(() => _photo = File(x.path));
    } finally {
      _taking = false;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const TopBar(
        title: 'Camera',
        actions: [DebugNavButton()],
      ),
      backgroundColor: AppTheme.tealBg,
      body: Center(
        child: AspectRatio(
          aspectRatio: 9 / 16,
          child: Container(
            clipBehavior: Clip.antiAlias,
            decoration: BoxDecoration(
              color: Colors.black,
              borderRadius: BorderRadius.circular(12),
            ),
            child: _photo == null
                ? const Center(
                    child: Icon(Icons.photo_camera_outlined,
                        size: 100, color: Colors.white54),
                  )
                : Image.file(_photo!, fit: BoxFit.cover),
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton.large(
        onPressed: _takePhoto,
        child: const Icon(Icons.camera_alt),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
    );
  }
}
