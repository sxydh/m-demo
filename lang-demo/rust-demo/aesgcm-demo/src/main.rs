use aes_gcm::{
    aead::{Aead, KeyInit}, Aes256Gcm,
    Nonce,
};
use anyhow::{anyhow, Result};
use base64::{engine::general_purpose, Engine as _};
use pbkdf2::pbkdf2_hmac;
use rand::{rngs::OsRng, RngCore};
use sha2::Sha256;
use std::io::{self, Write};

const NONCE_SIZE: usize = 12;
const SALT_SIZE: usize = 16;
const KEY_SIZE: usize = 32;
const PBKDF2_ROUNDS: u32 = 100_000;

fn get_key(password: &str, salt: &[u8]) -> [u8; KEY_SIZE] {
    let mut key = [0u8; KEY_SIZE];
    pbkdf2_hmac::<Sha256>(password.as_bytes(), salt, PBKDF2_ROUNDS, &mut key);
    key
}

fn encrypt_string(password: &str, plaintext: &str) -> Result<String> {
    let mut salt = [0u8; SALT_SIZE];
    let mut nonce = [0u8; NONCE_SIZE];
    OsRng.fill_bytes(&mut salt);
    OsRng.fill_bytes(&mut nonce);

    let key = get_key(password, &salt);
    let cipher = Aes256Gcm::new_from_slice(&key).map_err(anyhow::Error::msg)?;

    let encrypted_bytes = cipher
        .encrypt(Nonce::from_slice(&nonce), plaintext.as_bytes())
        .map_err(anyhow::Error::msg)?;

    let mut combined_bytes = Vec::new();
    combined_bytes.extend_from_slice(&salt);
    combined_bytes.extend_from_slice(&nonce);
    combined_bytes.extend_from_slice(&encrypted_bytes);

    Ok(general_purpose::STANDARD.encode(&combined_bytes))
}

fn decrypt_string(password: &str, encrypted_b64: &str) -> Result<String> {
    let combined_bytes = general_purpose::STANDARD.decode(encrypted_b64)?;

    if combined_bytes.len() < SALT_SIZE + NONCE_SIZE {
        return Err(anyhow!("Invalid encrypted data format"));
    }

    let (salt, rest) = combined_bytes.split_at(SALT_SIZE);
    let (nonce, encrypted_bytes) = rest.split_at(NONCE_SIZE);

    let key = get_key(password, salt);
    let cipher = Aes256Gcm::new_from_slice(&key).map_err(anyhow::Error::msg)?;
    let decrypted_bytes = cipher
        .decrypt(Nonce::from_slice(nonce), encrypted_bytes)
        .map_err(anyhow::Error::msg)?;

    Ok(String::from_utf8(decrypted_bytes)?)
}

fn main() -> Result<()> {
    loop {
        print!("Encryption password: ");
        io::stdout().flush()?;
        let mut encrypt_password = String::new();
        io::stdin().read_line(&mut encrypt_password)?;
        let encrypt_password = encrypt_password.trim().to_string();

        print!("Decryption password: ");
        io::stdout().flush()?;
        let mut decrypt_password = String::new();
        io::stdin().read_line(&mut decrypt_password)?;
        let decrypt_password = decrypt_password.trim().to_string();

        print!("Input:\n");
        io::stdout().flush()?;
        let mut input = String::new();
        io::stdin().read_line(&mut input)?;
        let input = input.trim();

        if !encrypt_password.is_empty() {
            match encrypt_string(&encrypt_password, input) {
                Ok(enc) => println!("\n{}\n", enc),
                Err(e) => eprintln!("\n{}\n", e),
            }
        } else if !decrypt_password.is_empty() {
            match decrypt_string(&decrypt_password, input) {
                Ok(dec) => println!("\n{}\n", dec),
                Err(e) => eprintln!("\n{}\n", e),
            }
        }
    }
}
