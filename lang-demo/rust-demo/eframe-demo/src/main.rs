use eframe::egui;

fn main() -> Result<(), eframe::Error> {
    eframe::run_simple_native("Hello egui", Default::default(), |ctx, _| {
        egui::CentralPanel::default().show(ctx, |ui| {
            ui.label("Hello, World!");
        });
    })
}
