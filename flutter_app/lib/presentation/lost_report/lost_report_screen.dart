import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../widgets/top_bar.dart';
import '../widgets/debug_nav.dart';
import '../../theme/app_theme.dart';
import '../../routes/app_routes.dart';

class LostReportScreen extends StatefulWidget {
  const LostReportScreen({super.key});
  @override
  State<LostReportScreen> createState() => _LostReportScreenState();
}

class _LostReportScreenState extends State<LostReportScreen> {
  final desc = TextEditingController();
  final place = TextEditingController();
  final date  = TextEditingController();

  File? _image;
  final List<String> _categorias = ['Electronics', 'Books', 'Clothing', 'ID'];
  String? _catSeleccionada;

  bool _pickingImage = false; // 👈 flag

Future<void> _pickImage() async {
  if (_pickingImage) return;         // evita doble tap
  _pickingImage = true;
  try {
    final x = await ImagePicker().pickImage(source: ImageSource.gallery);
    if (!mounted) return;
    if (x != null) setState(() => _image = File(x.path));
  } finally {
    _pickingImage = false;
  }
}


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const TopBar(title: 'Report a lost item', actions: [DebugNavButton()]),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            GestureDetector(
              onTap: _pickImage,
              child: Container(
                height: 180,
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: Colors.black12),
                ),
                child: _image == null
                    ? const Center(child: Icon(Icons.cloud_upload, size: 70))
                    : ClipRRect(
                        borderRadius: BorderRadius.circular(16),
                        child: Image.file(_image!, fit: BoxFit.cover),
                      ),
              ),
            ),
            const SizedBox(height: 16),

            TextField(controller: desc, decoration: const InputDecoration(hintText: 'Description')),
            const SizedBox(height: 12),
            TextField(controller: place, decoration: const InputDecoration(hintText: 'Place')),
            const SizedBox(height: 12),
            TextField(controller: date, decoration: const InputDecoration(hintText: 'Date')),
            const SizedBox(height: 16),

            const Text('Category', style: TextStyle(fontWeight: FontWeight.w700)),
            Wrap(
              spacing: 8,
              children: _categorias.map((c) {
                final selected = _catSeleccionada == c;
                return ChoiceChip(
                  label: Text(c),
                  selected: selected,
                  onSelected: (v) => setState(() => _catSeleccionada = v ? c : null),
                );
              }).toList(),
            ),

            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(child: ElevatedButton(onPressed: () => Navigator.pop(context), child: const Text('Exit'))),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.pushNamed(
                        context,
                        AppRoutes.matchDetail,
                        arguments: _image?.path,
                      );
                    },
                    child: const Text('Save'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
