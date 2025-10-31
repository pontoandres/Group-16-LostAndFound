import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import '../../viewmodels/lost_report/lost_report_viewmodel.dart';

class LostReportScreen extends StatelessWidget {
  const LostReportScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => LostReportViewModel(),
      child: const _LostReportBody(),
    );
  }
}

class _LostReportBody extends StatelessWidget {
  const _LostReportBody();

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<LostReportViewModel>();

    return Scaffold(
      appBar: AppBar(title: const Text('Report lost item')),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () async {
            await vm.categoriesFuture;
          },
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: vm.titleCtrl,
                      decoration: InputDecoration(
                        labelText: 'Title',
                        suffixIcon: IconButton(
                          onPressed: vm.isLoading ? null : () => vm.suggestSmartTitle(),
                          icon: const Icon(Icons.auto_fix_high),
                          tooltip: 'Smart title',
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              TextField(
                controller: vm.descriptionCtrl,
                maxLines: 4,
                maxLength: 300,
                decoration: const InputDecoration(labelText: 'Description'),
              ),
              const SizedBox(height: 6),
              StreamBuilder<int>(
                stream: vm.descRemainingStream,
                initialData: 300,
                builder: (context, snap) {
                  final v = snap.data ?? 0;
                  final ok = v >= 0;
                  return Align(
                    alignment: Alignment.centerRight,
                    child: Text(
                      '$v',
                      style: TextStyle(
                        color: ok ? Colors.grey[700] : Colors.red,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              FutureBuilder<List<String>>(
                future: vm.categoriesFuture,
                builder: (context, snapshot) {
                  final items = snapshot.data ?? const <String>[];
                  return InputDecorator(
                    decoration: const InputDecoration(labelText: 'Category'),
                    child: DropdownButtonHideUnderline(
                      child: DropdownButton<String>(
                        isExpanded: true,
                        value: items.contains(vm.categoryCtrl.text) && vm.categoryCtrl.text.isNotEmpty
                            ? vm.categoryCtrl.text
                            : (items.isNotEmpty ? items.first : null),
                        items: items.map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
                        onChanged: vm.isLoading
                            ? null
                            : (v) {
                                if (v == null) return;
                                vm.categoryCtrl.text = v;
                                FocusScope.of(context).unfocus();
                              },
                      ),
                    ),
                  );
                },
              ),
              const SizedBox(height: 12),
              TextField(
                controller: vm.locationCtrl,
                decoration: const InputDecoration(labelText: 'Location'),
              ),
              const SizedBox(height: 12),
              ListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('Date and time lost'),
                subtitle: Text(vm.lostAt?.toLocal().toString() ?? ''),
                trailing: IconButton(
                  onPressed: vm.isLoading
                      ? null
                      : () async {
                          final now = DateTime.now();
                          final d = await showDatePicker(
                            context: context,
                            firstDate: DateTime(now.year - 1),
                            lastDate: DateTime(now.year + 1),
                            initialDate: vm.lostAt ?? now,
                          );
                          if (d == null) return;
                          final t = await showTimePicker(
                            context: context,
                            initialTime: TimeOfDay.fromDateTime(vm.lostAt ?? now),
                          );
                          final dt = DateTime(
                            d.year,
                            d.month,
                            d.day,
                            (t?.hour ?? 0),
                            (t?.minute ?? 0),
                          );
                          vm.setLostAt(dt);
                        },
                  icon: const Icon(Icons.edit_calendar),
                ),
              ),
              const SizedBox(height: 12),
              _ImagePickerRow(
                imageBytes: vm.imageBytes,
                onPickCamera: vm.isLoading ? null : () => vm.pickImage(source: ImageSource.camera),
                onPickGallery: vm.isLoading ? null : () => vm.pickImage(source: ImageSource.gallery),
                onClear: vm.isLoading
                    ? null
                    : () {
                        vm.imageBytes = null;
                        vm.notifyListeners();
                      },
              ),
              const SizedBox(height: 20),
              SizedBox(
                height: 52,
                child: ElevatedButton(
                  onPressed: vm.isLoading
                      ? null
                      : () async {
                          final ok = await vm.submit();
                          if (ok && context.mounted) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text('Reported')),
                              );
                              Navigator.pop(context, true);
                          } else if (vm.error != null && context.mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text(vm.error!)),
                            );
                          }
                        },
                  child: vm.isLoading
                      ? const CircularProgressIndicator()
                      : const Text('Submit'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ImagePickerRow extends StatelessWidget {
  final Uint8List? imageBytes;
  final VoidCallback? onPickCamera;
  final VoidCallback? onPickGallery;
  final VoidCallback? onClear;

  const _ImagePickerRow({
    required this.imageBytes,
    required this.onPickCamera,
    required this.onPickGallery,
    required this.onClear,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 84,
          height: 84,
          decoration: BoxDecoration(
            color: Colors.grey.shade200,
            borderRadius: BorderRadius.circular(8),
            border: Border.all(color: Colors.grey.shade400),
          ),
          clipBehavior: Clip.antiAlias,
          child: imageBytes == null
              ? const Icon(Icons.image, size: 36)
              : Image.memory(imageBytes!, fit: BoxFit.cover),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              ElevatedButton.icon(
                onPressed: onPickCamera,
                icon: const Icon(Icons.photo_camera),
                label: const Text('Camera'),
              ),
              ElevatedButton.icon(
                onPressed: onPickGallery,
                icon: const Icon(Icons.photo_library),
                label: const Text('Gallery'),
              ),
              ElevatedButton.icon(
                onPressed: onClear,
                icon: const Icon(Icons.clear),
                label: const Text('Clear'),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
