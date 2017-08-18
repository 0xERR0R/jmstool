import { AbbreviatePipe } from './abbreviate.pipe';

describe('AbbreviatePipe', () => {
  const pipe = new AbbreviatePipe();
  it('create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('short text with default parameter', () => {
    expect(pipe.transform('abc')).toBe('abc');
  });

  it('long text with default parameter shoud be truncated', () => {
    expect(pipe.transform('Lorem ipsum dolor sit amet, consetetur sadipscing elitr,' +
      ' sed diam nonumy eirmod tempor invidunt ut labore')).toBe('Lorem ipsum dolor sit amet,' +
      ' consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut l...');
  });


  it('long text with given parameter shoud be truncated', () => {
    expect(pipe.transform('Lorem ipsum dolor sit amet', [5])).toBe('Lorem...');
  });
});
