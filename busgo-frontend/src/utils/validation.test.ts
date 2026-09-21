import { describe, expect, it } from 'vitest'
import {
  validateEmail,
  validateLoginPassword,
  validateName,
  validateNewPassword,
  validatePasswordMatch,
} from './validation'

describe('validateEmail', () => {
  it('accepts a normal address', () => {
    expect(validateEmail('jane@example.com')).toBeNull()
  })
  it('rejects empty and malformed addresses', () => {
    expect(validateEmail('   ')).not.toBeNull()
    expect(validateEmail('jane@')).not.toBeNull()
    expect(validateEmail('jane example.com')).not.toBeNull()
  })
})

describe('validateNewPassword', () => {
  it('requires 8 to 72 characters', () => {
    expect(validateNewPassword('')).not.toBeNull()
    expect(validateNewPassword('short')).not.toBeNull()
    expect(validateNewPassword('long-enough')).toBeNull()
    expect(validateNewPassword('x'.repeat(73))).not.toBeNull()
  })
})

describe('other validators', () => {
  it('requires a name', () => {
    expect(validateName('  ')).not.toBeNull()
    expect(validateName('Jane')).toBeNull()
  })
  it('requires a login password', () => {
    expect(validateLoginPassword('')).not.toBeNull()
    expect(validateLoginPassword('anything')).toBeNull()
  })
  it('checks that passwords match', () => {
    expect(validatePasswordMatch('abc12345', 'abc12345')).toBeNull()
    expect(validatePasswordMatch('abc12345', 'different')).not.toBeNull()
  })
})
