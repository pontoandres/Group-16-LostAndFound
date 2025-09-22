import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../theme/app_theme.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});
  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final name = TextEditingController(text: 'Pepe Pepeins');
  final email = TextEditingController(text: 'pepe@uniandes.edu.co');

  File? _photo;

bool _pickingPhoto = false;

Future<void> _pickPhoto() async {
  if (_pickingPhoto) return;         
  _pickingPhoto = true;
  try {
    final x = await ImagePicker().pickImage(source: ImageSource.gallery);
    if (!mounted) return;
    if (x != null) setState(() => _photo = File(x.path));
  } finally {
    _pickingPhoto = false;
  }
}


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const TopBar(title: 'Profile', actions: [DebugNavButton()]),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            const SizedBox(height: 8),
            Center(
              child: Stack(
                children: [
                  CircleAvatar(
                    radius: 60,
                    backgroundColor: Colors.white,
                    backgroundImage: _photo != null ? FileImage(_photo!) : null,
                    child: _photo == null ? const Icon(Icons.person, size: 60) : null,
                  ),
                  Positioned(
                    right: 0,
                    bottom: 0,
                    child: InkWell(
                      onTap: _pickPhoto,
                      child: Container(
                        padding: const EdgeInsets.all(8),
                        decoration: BoxDecoration(
                          color: AppTheme.orange,
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.edit, size: 20),
                      ),
                    ),
                  )
                ],
              ),
            ),
            const SizedBox(height: 16),
            _pill('Full Name: ${name.text}'),
            const SizedBox(height: 12),
            _pill('Email: ${email.text}'),
            const SizedBox(height: 12),
            const _PillStatic(text: 'Published objects: 1000'),
            const SizedBox(height: 24),
            Row(
              children: [
                Expanded(child: OutlinedButton(onPressed: () => Navigator.pop(context), child: const Text('Back'))),
                const SizedBox(width: 12),
                Expanded(child: ElevatedButton(onPressed: () {}, child: const Text('Save'))),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _pill(String text) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    decoration: BoxDecoration(color: AppTheme.pill, borderRadius: BorderRadius.circular(18)),
    child: Text(text, style: const TextStyle(fontWeight: FontWeight.w600)),
  );
}

class _PillStatic extends StatelessWidget {
  final String text;
  const _PillStatic({required this.text});
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    decoration: BoxDecoration(color: AppTheme.pill, borderRadius: BorderRadius.circular(18)),
    child: Text(text, style: const TextStyle(fontWeight: FontWeight.w600)),
  );
}
