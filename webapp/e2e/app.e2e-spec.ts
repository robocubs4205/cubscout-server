import { CubscoutWebappPage } from './app.po';

describe('cubscout-webapp App', () => {
  let page: CubscoutWebappPage;

  beforeEach(() => {
    page = new CubscoutWebappPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
